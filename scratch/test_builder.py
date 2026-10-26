import copy
import docx
from docx.shared import Pt, Inches, RGBColor

def set_cell_text_and_font(cell, text, font_name="Times New Roman", font_size=Pt(12), bold=False):
    cell.text = text
    for p in cell.paragraphs:
        p.paragraph_format.line_spacing = 1.15
        p.paragraph_format.space_before = Pt(2)
        p.paragraph_format.space_after = Pt(2)
        for r in p.runs:
            r.font.name = font_name
            r.font.size = font_size
            r.font.bold = bold

def populate_nested_table(nt, data_rows, font_name="Times New Roman", font_size=Pt(11)):
    # Header is row 0
    # Current data rows: nt.rows[1:]
    # If we have more data rows than existing, clone row 1
    sample_tr = copy.deepcopy(nt.rows[1]._tr)
    
    # Remove all rows from index 1
    while len(nt.rows) > 1:
        nt._tbl.remove(nt.rows[-1]._tr)
        
    # Now add each data row
    for item in data_rows:
        new_tr = copy.deepcopy(sample_tr)
        nt._tbl.append(new_tr)
        last_row = nt.rows[-1]
        set_cell_text_and_font(last_row.cells[0], str(item[0]), font_name, font_size, bold=False)
        set_cell_text_and_font(last_row.cells[1], str(item[1]), font_name, font_size, bold=False)
        set_cell_text_and_font(last_row.cells[2], str(item[2]), font_name, font_size, bold=False)

def build_use_case_table(tmpl_tbl, uc_data):
    tbl_elm = copy.deepcopy(tmpl_tbl._tbl)
    # Wrap in Table object
    doc_temp = docx.Document()
    doc_temp._body._body.append(tbl_elm)
    t = doc_temp.tables[0]
    
    fn = "Times New Roman"
    fz = Pt(12)
    
    # Row 0: UC Code and Name
    set_cell_text_and_font(t.rows[0].cells[1], uc_data['code'], fn, fz, bold=True)
    set_cell_text_and_font(t.rows[0].cells[3], uc_data['name'], fn, fz, bold=True)
    
    # Row 1: Actor
    set_cell_text_and_font(t.rows[1].cells[1], uc_data['actor'], fn, fz)
    
    # Row 2: Description
    set_cell_text_and_font(t.rows[2].cells[1], uc_data['description'], fn, fz)
    
    # Row 3: Trigger
    set_cell_text_and_font(t.rows[3].cells[1], uc_data['trigger'], fn, fz)
    
    # Row 4: Precondition
    set_cell_text_and_font(t.rows[4].cells[1], uc_data['precondition'], fn, fz)
    
    # Row 5: Main flow (nested table 0 in cell 1)
    nt_main = t.rows[5].cells[1].tables[0]
    populate_nested_table(nt_main, uc_data['main_flow'], fn, Pt(11))
    
    # Row 6: Alternative flow (nested table 0 in cell 1)
    nt_alt = t.rows[6].cells[1].tables[0]
    populate_nested_table(nt_alt, uc_data['alt_flow'], fn, Pt(11))
    
    # Row 7: Postcondition
    set_cell_text_and_font(t.rows[7].cells[1], uc_data['postcondition'], fn, fz)
    
    return t._tbl

print("Helper functions compiled successfully!")
