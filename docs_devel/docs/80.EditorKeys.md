# ﻿This short text describes key behavior in the editor.

## Terms:

"move to inside segment" means that caret will be moved to the begin of segment if previous position was before segment, and to the end of segment, if previous position was after segment.

## Keys definitions:

Keys between parenthesis indicate Mac specific keys.

### Navigation
- Left: one char left, but not earlier than segment's begin
- Right: one char right, but not later than segment's end
- Ctrl+Left (Alt+Left): one word (or tag) left, but stay within the segment (Mac: the caret moves to the leftmost side of the word)
- Ctrl+Right (Alt+Right): one word (or tag) right, but stay within the segment (Mac: as above)

- Home (Cmd+Left): to the begin of segment's line
- End (Cmd+Right): to the end of segment's line

- Ctrl+Home (Cmd+Up): to the beginning of the segment
- Ctrl+End (Cmd+Down): to the end of the segment

- Ctrl+PgUp (Cmd+Fn+Up): to the beginning of the document
- Ctrl+PgDn (Cmd+Fn+Down): to the end of the document

- PgUp (Fn+Up): move to one page up
- PgDn (Fn+Down): move to one page down

When the caret is outside the editable segment:
- Home (Up): "move to inside segment"
- End (Down): "move to inside segment"


### Deletion
- Backspace (Delete): remove char before caret
- Ctrl+H: same as Backspace
- Delete (Fn+Delete / "Delete ⌦" on full keyboards): remove char after caret

When the caret is outside the editable segment:
- Backspace (Delete): nothing
- Ctrl+H: same as Backspace
- Delete (Fn+Delete / "Delete ⌦"): nothing
- Any char key, if clicked outside editable segment, will be ignored.

- Ctrl+Backspace (Alt+Delete): remove to the begin of word
- Ctrl+Delete (Alt+Fn+Delete / Alt+"Delete ⌦"): remove to the begin of next word


### Other
- Ctrl (Cmd)+Enter: previous segment
- Ctrl (Cmd)+A: select full editable segment
- Shift+Ctrl+O: RTL-LTR switch -> disabled in 5.8+


### Shift
"Shift" key doesn't have own behavior - it just adds selection from old to new caret position.
All keys should move caret with the "Shift", like without "Shift"

Commands, which works even if current selection is outside the editable segment. In this case selection will be corrected to segment's boundaries:
- Paste: Ctrl (Cmd)+V / Ctrl+Insert (No Insert key on Mac)
- Cut: Ctrl (Cmd)+X / Ctrl+Delete  (No effect on Mac)
- Insert match: Ctrl (Cmd)+I
- Insert source: Shift+Ctrl (Cmd)+I
