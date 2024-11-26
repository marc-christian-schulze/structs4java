- generate new GPG key: \
  ```gpg --gen-key```
- export private key: \
  ```gpg --export-secret-keys -a $KEY_ID | tr '\n' ',' | sed -e 's#,#\\n#g'```
- change GitHub secrets:
  * OSSRH_GPG_SECRET_KEY to the exported content from above
  * OSSRH_GPG_SECRET_KEY_PASSWORD to the passphrase used during key generation
- publish public key (cf. https://central.sonatype.org/publish/requirements/gpg/#signing-a-file):
  - ```gpg --keyserver keyserver.ubuntu.com --send-keys $KEY_ID```
  - ```gpg --keyserver keys.openpgp.org --send-keys $KEY_ID```
  - ```gpg --keyserver pgp.mit.edu --send-keys $KEY_ID```
