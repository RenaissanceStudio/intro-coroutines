package tasks

import contributors.GitHubService
import contributors.RequestData
import contributors.User
import kotlinx.coroutines.*
import samples.log

suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> = coroutineScope {
    return@coroutineScope withContext(Dispatchers.IO) {

        val repos = service.getOrgRepos(req.org).bodyList()
        val deferreds: List<Deferred<List<User>>> = repos.map { repo ->
            async { service.getRepoContributors(req.org, repo.name)
                .also { log("Executing on thread : ${Thread.currentThread().name}") }
                .bodyList() }
        }
        deferreds.awaitAll().flatten().aggregate()
    }
}