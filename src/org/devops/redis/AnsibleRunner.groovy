package org.devops.redis

import groovy.transform.Field

class AnsibleRunner implements Serializable {

    private static final long serialVersionUID = 1L

    def script
    String playbookPath
    String inventoryPath
    String vaultCredId
    String sshCredId

    AnsibleRunner(def script, Map config) {
        this.script       = script
        this.playbookPath  = config.playbookPath  ?: 'playbooks/redis.yml'
        this.inventoryPath = config.inventoryPath ?: 'inventories/dev'
        this.vaultCredId  = config.vaultCredId   ?: 'ansible-vault-dev'
        this.sshCredId    = config.sshCredId     ?: 'ssh-dev-key'
    }

    /**
     * Run an Ansible playbook with optional extra vars and tags.
     * @param extraVars  Map of Ansible extra variables
     * @param tags       Comma-separated list of tags (optional)
     */
    def run(Map extraVars = [:], String tags = '') {
        script.withCredentials([
            script.sshUserPrivateKey(
                credentialsId: sshCredId,
                keyFileVariable: 'SSH_KEY'
            ),
            script.string(
                credentialsId: vaultCredId,
                variable: 'VAULT_PASS'
            )
        ]) {
            def extraVarsStr = extraVars
                .collect { k, v -> "-e \"${k}=${v}\"" }
                .join(' ')

            def tagsStr = tags ? "--tags ${tags}" : ''

            script.sh """
                ansible-playbook \\
                  -i ${inventoryPath}/hosts.ini \\
                  --private-key \$SSH_KEY \\
                  --vault-password-file <(echo \$VAULT_PASS) \\
                  ${extraVarsStr} ${tagsStr} \\
                  ${playbookPath}
            """
        }
    }

    /** Dry-run mode — syntax check only, no changes applied. */
    def syntaxCheck() {
        script.sh """
            ansible-playbook --syntax-check \\
              -i ${inventoryPath}/hosts.ini \\
              ${playbookPath}
        """
    }
}
