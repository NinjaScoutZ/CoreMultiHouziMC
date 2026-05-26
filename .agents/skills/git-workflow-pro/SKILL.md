---
name: git-workflow-pro
description: Professional playbook for git branch management, conventional commits, code review checklists, and clean merge conflict resolution.
---

# Git Workflow & Commit Guidelines

> **Target Standard:** Conventional Commits v1.0.0
> **Scope:** Multi-developer branches, pull requests, release tagging, and clean commit history.

---

## 1. Branch Naming Convention

All workspace branches must follow a structured naming format prefix:

| Prefix | Use Case | Example |
|---|---|---|
| `feat/` | New features or APIs | `feat/skywars-elo-system` |
| `fix/` | Bug fixes and stabilization | `fix/scoreboard-flickering` |
| `refactor/` | Code structure improvements without behavioral changes | `refactor/shop-gui-base` |
| `docs/` | Documentation edits and guidelines | `docs/paper-26-1-api` |
| `perf/` | Performance optimizations and latency fixes | `perf/entity-tracker-ticks` |

---

## 2. Conventional Commit Messages

Every commit must use a clear, structured message format:
```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

### 2.1 Commit Types
- **`feat`**: A new feature.
- **`fix`**: A bug fix.
- **`refactor`**: A code change that neither fixes a bug nor adds a feature.
- **`perf`**: A code change that improves performance.
- **`docs`**: Documentation-only changes.
- **`style`**: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc).
- **`test`**: Adding missing tests or correcting existing tests.
- **`chore`**: Changes to the build process or auxiliary tools/libraries (e.g. updating Maven dependencies).

### 2.2 Scope (Optional but Recommended)
The module or component affected, enclosed in parentheses.
*Examples:* `feat(lobby): add welcome dialogue`, `fix(arcade/skywars): fix spectator location`.

### 2.3 Breaking Changes
Must be indicated by a `!` after the type/scope or by adding `BREAKING CHANGE:` in the commit footer.
*Example:* `refactor(shared)!: rename ItemStackFactory methods`.

---

## 3. Commit Checklist (Pre-Commit Review)

Before committing any code or proposing a pull request, run through the following checks:

- [ ] **No Secrets**: Verify that no API keys, credentials, or development passwords are in the modified code.
- [ ] **No Debug Traces**: Ensure that temporary console print messages (`System.out.println`, `Bukkit.broadcastMessage("debug")`, stack trace prints) are removed or replaced with proper logger statements (`plugin.getLogger().info(...)`).
- [ ] **Clean Diff**: Check `git diff` to ensure there are no unintended formatting changes, trailing whitespaces, or unused imports included.
- [ ] **Compile Success**: Confirm that the code compiles successfully without errors or warnings.
- [ ] **Single Intent**: Keep commits atomic. Ensure a single commit contains changes related to only one feature, bug fix, or refactoring intent.

---

## 4. Conflict Resolution Strategy

When merge conflicts occur during rebase or merge:

1. **Identify Conflict Areas**: Use `git status` to find all conflicted files.
2. **Review Changes Jointly**: Compare the incoming changes (`THEIRS`) with your local modifications (`OURS`).
3. **Resolve Manually**: Do not use automated overwrite tools (like `git checkout --ours`). Open the files and merge the logic to ensure that existing project integration paths are supported.
4. **Compile and Test**: Rebuild the project immediately after conflict resolution to ensure that the merged state compiles and runs cleanly.
5. **Continue Rebase/Commit**: Run `git add <resolved-files>` and complete the rebase (`git rebase --continue`) or merge.
