# Segment Sorting — Design & Development Log

This package implements **decoupled filtering and sorting of editor segments** for
OmegaT, addressing SourceForge feature request
[#1094 "Sorting segments"](https://sourceforge.net/p/omegat/feature-requests/1094/).

Until now the editor's displayed segment list (`m_docSegList` in
`EditorController`) was built in `loadDocument()` by iterating `file.entries` in
natural file order, optionally applying an `IEditorFilter`, and emitting the
segments in exactly that order. Filtering and ordering were coupled and the order
was always the file order. This feature separates the two concerns so the display
order can be changed live, without affecting which segments are visible.

## Architecture

- **`IEditorFilter`** (unchanged) decides *which* segments are shown.
- **`IEditorSorter`** (new) decides *in which order* the already-filtered segments
  are shown. It exposes a single `Comparator<SegmentBuilder> getComparator()`.
- **`SortKey`** (new enum) is one sort criterion. Each constant defines an
  *ascending* comparator; descending is derived centrally via
  `comparator(Collator, boolean)`. Adding a criterion = adding one enum constant.
  Text keys collate with a `java.text.Collator` built for the **source language**,
  so accents and language-specific ordering are respected.
- **`MultiKeySorter`** (new) implements `IEditorSorter` by chaining a list of
  `KeySpec` (key + direction) via `thenComparing`, with a stable natural-order
  (`entryNum`) tiebreaker so the result is fully deterministic. It also offers
  `toPreferenceString` / `fromPreferenceString` serialization helpers.
- **`SortBar`** (new Swing component) is the always-available control bar.
- **`EditorController`** wiring:
  - `loadDocument()` applies the sorter **after** filtering (`tmpSegList.sort(...)`).
    With no sorter the order is byte-identical to before (no regression).
  - `setSort` / `getSort` / `removeSort` were added to `IEditor`, mirroring the
    filter API; the document is reloaded live and the current entry is preserved.
  - The sort and filter bars are stacked in a north container (`northBars`); the
    sort bar sits **above** the filter bar.
  - The sort bar is shown only when a project is open and there is **more than one**
    segment after filtering (sorting a single segment is pointless).

### Order-independence fix (important)

`gotoEntry(int)` and the `setFilter` fallback previously assumed `m_docSegList`
was sorted ascending by `segmentNumberInProject` (a `>= entryNum` linear search).
Once the list can be reordered, that assumption breaks. Both were changed to
locate segments by **exact `entryNum`** (with a nearest-entry fallback), making
them order-independent. Index-based navigation (`iterateToEntry` next/prev) is
intentionally left as-is so it now follows the visible sort order.

## Sort criteria

`File order (unsorted)` (default) · Source alphabetical / reverse "rhyme" / length ·
Target alphabetical / reverse / length · Translation status · Note alphabetical /
reverse / length · Comment alphabetical / reverse / length · Change date ·
Creation date · Modification author · Creation author. Each (except file order)
can be ascending or descending.

Note the distinction:
- **Comment** = read-only comment from the source document
  (`SourceTextEntry.getComment()`).
- **Note** = the translator's own note on the segment (`TMXEntry.getNote()`).

## UI behaviour

- Always-visible bar above the editor (no menu needed), centered in its row.
- One criterion by default. A **`+`** button adds a secondary, then a tertiary
  criterion (max 3); the third row has no `+`. Rows 2 and 3 have a **`−`** button
  to remove that criterion. The first row has no `−`.
- With more than one criterion the rows are labelled *Primary / Secondary /
  Tertiary sort key*.
- The `+` is disabled while the primary key is "file order (unsorted)", since a
  secondary criterion is meaningless without a primary sort.
- When a sort is active, a notice that the segment numbers are no longer
  sequential appears on its own line above the controls.

## Contributing this feature

OmegaT tracks **tickets on SourceForge** but hosts **code and pull requests on
GitHub** (`omegat-org/omegat`). So:

1. Assign / comment on SourceForge ticket #1094 to claim it.
2. Push the `feature/1094-segment-sorting` branch to your GitHub fork and open a
   pull request against `omegat-org/omegat`, referencing the SourceForge ticket in
   the description.

## Development log

The feature was built iteratively. The rounds below summarize the discussion that
shaped it (timestamps removed).

**Initial request.** Separate filtering and sorting of segments so the display
order can be changed dynamically/live.

**Research.** A check of the SourceForge feature-request tracker found the open
ticket #1094 "Sorting segments", which matches this work almost exactly: multiple
combinable criteria, ascending/descending, a remove control mirroring the filter,
and the explicit requirement that sorting must cooperate with (not replace)
filtering.

**Round 1 — criteria & multi-key.** Requested combinable primary/secondary/
tertiary sorting and criteria: file order (default), source alphabetical (asc/desc),
reverse-string "rhyming dictionary" (asc/desc), length, translation status. A
multi-disciplinary review (developers, localizers, UX) was requested to decide what
else makes sense.

**Round 2 — target & comment.** Added sorting by target text and by comment in
addition to source.

**Round 3 — UX streamlining.** The sort bar should always be visible (no menu);
the default option should read "File order (unsorted)" and the empty "—" entry was
dropped; the secondary/tertiary UI was temporarily removed; the bar was centered
and moved above the filter bar; added comment reverse ("rhyme") and comment length.

**Round 4 — dynamic multi-key & more criteria.** The bar must be hidden when no
project is open or when one or fewer segments remain after filtering. "Has note"
was dropped. Added change date, creation date, modification author, creation author
(all asc/desc) and note alphabetical / reverse / length. The renumbering notice was
moved to its own line above the controls. A `+` button adds another criterion (up
to three; primary/secondary/tertiary labels); the first row has no `−`, rows 2 and
3 do. During this round the earlier mislabeling of "comment" (which had been mapped
to the note) was corrected: comment now comes from the source document and note
from the translator's note.

**Round 5 — team-feedback follow-up.** The agreed UX refinement was implemented:
the `+` button is disabled while the primary key is "file order (unsorted)".

## Tests

`MultiKeySorterTest` covers the comparator chaining (single key asc/desc, multi-key
with a secondary breaking primary ties, the stable natural tiebreaker), the
source-based criteria, and the preference-string round-trip including empty/unknown
handling.
