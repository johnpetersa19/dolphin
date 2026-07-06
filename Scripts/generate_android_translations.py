#!/usr/bin/env python3
"""
Script para sincronizar traduções desktop (.po) para Android (strings XML localizadas).
Extrai strings do Android base (EN) e das traduções desktop, gerando arquivos values-*.
"""

import os
import re
import subprocess
from pathlib import Path
from typing import Dict, Tuple

# Mapeamento de códigos de idioma PO -> Android
LANG_MAPPING = {
    'pt_BR': ('values-pt-rBR', 'pt_BR.po'),
    'pt': ('values-pt', 'pt.po'),
    'es': ('values-es', 'es.po'),
    'fr': ('values-fr', 'fr.po'),
    'de': ('values-de', 'de.po'),
    'it': ('values-it', 'it.po'),
    'nl': ('values-nl', 'nl.po'),
    'ja': ('values-ja', 'ja.po'),
    'zh_CN': ('values-zh-rCN', 'zh_CN.po'),
    'zh': ('values-zh-rCN', 'zh_CN.po'),
    'zh_TW': ('values-zh-rTW', 'zh_TW.po'),
    'ko': ('values-ko', 'ko.po'),
    'ru': ('values-ru', 'ru.po'),
    'tr': ('values-tr', 'tr.po'),
}

REPO_ROOT = Path(__file__).parent.parent
ANDROID_RES = REPO_ROOT / 'Source/Android/app/src/main/res'
LANGUAGES_DIR = REPO_ROOT / 'Languages/po'

def extract_strings_from_xml(xml_path: Path) -> Dict[str, str]:
    """Extrai strings do arquivo XML (nome -> valor)."""
    strings = {}
    if not xml_path.exists():
        return strings
    
    with open(xml_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Pattern para: <string name="key">value</string>
    pattern = r'<string\s+name="([^"]+)">([^<]*)</string>'
    for match in re.finditer(pattern, content):
        name, value = match.groups()
        strings[name] = value
    
    return strings

def extract_po_translations(po_path: Path) -> Dict[str, str]:
    """Extrai traducciones del archivo .po (msgid -> msgstr)."""
    translations = {}
    if not po_path.exists():
        return translations
    
    with open(po_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    i = 0
    while i < len(lines):
        line = lines[i].strip()
        
        # Buscar msgid
        if line.startswith('msgid "'):
            msgid_match = re.match(r'msgid "(.+)"', line)
            if msgid_match:
                msgid = msgid_match.group(1)
                
                # Buscar msgstr en la siguiente línea
                i += 1
                if i < len(lines):
                    msgstr_line = lines[i].strip()
                    if msgstr_line.startswith('msgstr "'):
                        msgstr_match = re.match(r'msgstr "(.+)"', msgstr_line)
                        if msgstr_match:
                            msgstr = msgstr_match.group(1)
                            if msgstr:  # Solo guardar si no está vacío
                                translations[msgid] = msgstr
        
        i += 1
    
    return translations

def escape_xml_value(value: str) -> str:
    """Escapa caracteres especiales para XML."""
    value = value.replace('&', '&amp;')
    value = value.replace('<', '&lt;')
    value = value.replace('>', '&gt;')
    value = value.replace('"', '&quot;')
    value = value.replace("'", "\\'")
    return value

def generate_localized_strings_xml(base_strings: Dict[str, str], 
                                  po_translations: Dict[str, str],
                                  lang_name: str) -> str:
    """Genera XML localizado combinando strings base + traducciones PO."""
    xml_lines = [
        '<?xml version="1.0" encoding="utf-8"?>',
        '<resources>',
        f'    <!-- Auto-generated translations for {lang_name} from desktop (.po files) -->',
    ]
    
    translated_count = 0
    for name, en_value in base_strings.items():
        # Buscar traducción en el archivo PO
        translated_value = po_translations.get(en_value, '')
        
        if translated_value:
            # Escapar el valor para XML
            escaped_value = escape_xml_value(translated_value)
            xml_lines.append(f'    <string name="{name}">{escaped_value}</string>')
            translated_count += 1
    
    xml_lines.append('</resources>')
    xml_lines.append('')  # Nueva línea al final
    
    print(f"[{lang_name}] Traducidas {translated_count}/{len(base_strings)} strings")
    
    return '\n'.join(xml_lines)

def main():
    print("=== Sincronizando traduções desktop (.po) para Android ===\n")
    
    # 1. Extrair strings base do Android (inglês)
    base_xml = ANDROID_RES / 'values/strings.xml'
    base_strings = extract_strings_from_xml(base_xml)
    print(f"[BASE] Carregados {len(base_strings)} strings base do Android\n")
    
    # 2. Processar cada idioma
    for lang_code, (values_dir, po_file) in LANG_MAPPING.items():
        po_path = LANGUAGES_DIR / po_file
        
        if not po_path.exists():
            print(f"[SKIP] {lang_code}: arquivo .po não encontrado ({po_file})")
            continue
        
        print(f"[PROCESS] {lang_code}:")
        
        # Extraer traducciones del .po
        po_translations = extract_po_translations(po_path)
        print(f"  - Carregadas {len(po_translations)} traduções do arquivo .po")
        
        # Generar XML localizado
        localized_xml = generate_localized_strings_xml(base_strings, po_translations, lang_code)
        
        # Crear directorio si no existe
        target_dir = ANDROID_RES / values_dir
        target_dir.mkdir(parents=True, exist_ok=True)
        
        # Guardar archivo
        target_file = target_dir / 'strings.xml'
        with open(target_file, 'w', encoding='utf-8') as f:
            f.write(localized_xml)
        
        print(f"  - Guardado em: {target_file.relative_to(REPO_ROOT)}\n")
    
    print("\n=== Conclusão ===")
    print("✓ Traduções sincronizadas com sucesso!")
    print(f"✓ Arquivos gerados em: Source/Android/app/src/main/res/values-*/")

if __name__ == '__main__':
    main()
