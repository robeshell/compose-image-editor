# Publishing

Two distribution channels are configured:

- **JitPack** — works out of the box, no secrets. Cutting a GitHub tag is enough.
- **Maven Central** (Central Portal) — requires a Sonatype account, a verified
  namespace, and a GPG signing key. Setup steps below.

The publishing config lives in [`imageeditor/build.gradle.kts`](imageeditor/build.gradle.kts)
(`mavenPublishing { … }`, powered by the `com.vanniktech.maven.publish` plugin).
Signing is **only** enabled when a signing key is present, so local builds and
JitPack (which have no key) are unaffected.

---

## JitPack (no setup)

1. Push a tag: `git tag 0.1.0 && git push origin 0.1.0`
2. (Optional) create a GitHub release for the tag.
3. JitPack builds on first request. Consumers:

   ```kotlin
   // settings.gradle.kts
   maven("https://jitpack.io")
   // build.gradle.kts
   implementation("com.github.robeshell:compose-image-editor:0.1.0")
   ```

---

## Maven Central (Central Portal)

Target coordinate: `io.github.robeshell:compose-image-editor:<version>`

### One-time setup

1. **Sonatype Central account** — sign in at <https://central.sonatype.com>.
2. **Verify the namespace `io.github.robeshell`** — add the namespace in the
   Central Portal; it verifies automatically against the matching GitHub account
   (`github.com/robeshell`).
3. **Generate a user token** — Central Portal → Account → Generate User Token.
   You get a username + password pair.
4. **GPG signing key**:
   ```bash
   gpg --gen-key
   gpg --list-secret-keys --keyid-format=long           # note the key id
   gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>   # publish public key
   gpg --armor --export-secret-keys <KEY_ID>            # ASCII-armored private key
   ```

### Credentials (never commit these)

Put them in `~/.gradle/gradle.properties` (user-global, outside the repo):

```properties
mavenCentralUsername=<central portal token username>
mavenCentralPassword=<central portal token password>

# ASCII-armored private key (escape newlines with \n, or use the env var form below)
signingInMemoryKey=-----BEGIN PGP PRIVATE KEY BLOCK-----\n...\n-----END PGP PRIVATE KEY BLOCK-----
signingInMemoryKeyPassword=<your gpg key passphrase>
```

Or via environment variables (CI-friendly):

```bash
export ORG_GRADLE_PROJECT_mavenCentralUsername=...
export ORG_GRADLE_PROJECT_mavenCentralPassword=...
export ORG_GRADLE_PROJECT_signingInMemoryKey="$(gpg --armor --export-secret-keys <KEY_ID>)"
export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword=...
```

### Release

1. Bump the version in `imageeditor/build.gradle.kts` (`coordinates(...)`).
2. Publish + auto-release:
   ```bash
   ./gradlew :imageeditor:publishAndReleaseToMavenCentral
   ```
   (or `publishToMavenCentral` to leave it in the Portal for manual release).
3. Verify on <https://central.sonatype.com> → Deployments. Artifacts appear on
   Maven Central within ~10–30 minutes.

> Without the signing key configured, `publishToMavenLocal` and JitPack builds
> still work — signing is skipped automatically.
