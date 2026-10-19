# UROOJ — Your Rise Begins

## Backup and recovery rule

- Keep a separate, dated copy of any known-good app backup outside the live deployment files.
- If the maintainer asks to **“Open From Backup”**, treat that as a recovery instruction: identify and use the exact saved backup provided by the maintainer, rather than assuming the current `main` branch is the backup.
- Never overwrite or delete the known-good backup while applying fixes.
- Before restoring a backup to `main`, compare it with the current version and confirm which files would be replaced. Preserve a copy of the current state so restoration is reversible.
- Do not claim a backup has been located or restored until its exact file/location is accessible and the resulting version has been verified.

## Live app

GitHub Pages: https://ideasbyhamza.github.io/UROOJ-Your-rise-begins./
