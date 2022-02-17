package tasks

import contributors.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
)  {
    withContext(Dispatchers.IO) {
        val repos = service.getOrgRepos(req.org).bodyList()
        val users = mutableListOf<User>()
        for ((index, repo:Repo) in repos.withIndex()) {
            val contributors = service.getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()

            users.addAll(contributors)

            val userList = users.aggregate()

            users.clear()
            users.addAll(userList)

            updateResults(userList, index == repos.lastIndex)
        }
    }
}
