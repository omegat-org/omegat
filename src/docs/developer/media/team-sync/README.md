# Team synchronization diagrams

Four SVG pages documenting the team project synchronization
(`org.omegat.core.team2`):

1. `01-team-sync-architecture.svg` - class relations and responsibilities,
   with the hook points of the sync robustness series marked.
2. `02-team-sync-flow.svg` - the autosave prepare/rebase/commit flow and its
   outcome matrix.
3. `03-team-sync-failure-chain.svg` - the three failure chains observed in the
   field and how the series addresses them.
4. `04-repository-migration.svg` - 3.x-era project layout vs. the current team
   layout, and why stale sync markers survive a rebuilt repository copy.

The pages are generated: edit `generate_team_sync_diagrams.py` and run it from
this directory with `python3 generate_team_sync_diagrams.py`; do not edit the
SVG files by hand.
