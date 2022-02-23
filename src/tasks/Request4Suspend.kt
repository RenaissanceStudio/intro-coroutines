package tasks

import contributors.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

suspend fun loadContributorsSuspend(service: GitHubService, req: RequestData): List<User> {
    return withContext(Dispatchers.IO) {
        val repos = service.getOrgRepos(req.org).bodyList()
        val users = mutableSetOf<User>()
        for (repo: Repo in repos) {
            val contributors = service.getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
            users.addAll(contributors)
        }
        log("Total: ${users.size} in thread : ${Thread.currentThread().name}")
        users.toList().aggregate()
    }
}