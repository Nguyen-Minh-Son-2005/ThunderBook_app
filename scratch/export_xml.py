import docx

doc = docx.Document(r'D:\HAUI_UNIVEERSITY\đặc tả use case.docx')
t = doc.tables[0]
with open(r'D:\HAUI_UNIVEERSITY\AppMangaFGroup\scratch\table_xml.xml', 'wb') as f:
    f.write(docx.oxml.tostring(t._tbl))
print("Saved table_xml.xml successfully, size:", len(docx.oxml.tostring(t._tbl)))
