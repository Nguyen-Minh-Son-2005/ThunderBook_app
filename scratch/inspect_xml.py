import docx

doc = docx.Document(r'D:\HAUI_UNIVEERSITY\đặc tả use case.docx')
t = doc.tables[0]
print("Outer table XML element:", t._tbl.tag)
for row in t.rows[:3]:
    print("Row height:", row.height)
    for cell in row.cells[:2]:
        print("  Cell width:", cell.width)
        tcPr = cell._tc.get_or_add_tcPr()
        print("  tcPr XML:", docx.oxml.xmlchemy.OxmlElement('dummy').fromstring(docx.oxml.tostring(tcPr)).xml if tcPr is not None else None)
