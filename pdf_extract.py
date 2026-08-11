from pathlib import Path
import pypdf
p = Path('Giới thiệu về Pando.pdf')
r = pypdf.PdfReader(str(p))
text = '\n'.join(page.extract_text() or '' for page in r.pages)
with open('pdf_text.txt', 'w', encoding='utf-8') as f:
    f.write(text)
print('wrote', len(text), 'chars')
