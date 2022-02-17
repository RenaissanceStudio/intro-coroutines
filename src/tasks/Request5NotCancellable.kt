package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

suspend fun loadContributorsNotCancellable(service: GitHubService, req: RequestData): List<User> = coroutineScope{
    val async = /*GlobalScope.*/async {
        log("starting loading for ${req.org}")
//        delay(3000)

        val repos = service.getOrgRepos(req.org).bodyList()
        val deferreds: List<Deferred<List<User>>> = repos.map { repo ->
            async {
                val users = service.getRepoContributors(req.org, repo.name).bodyList()

                log("contributors for ${repo.name} : ${users.size}")
                users
            }
        }
        val aggregate = deferreds.awaitAll().flatten().aggregate()
        aggregate
    }
    async.await()
}