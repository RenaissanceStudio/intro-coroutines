package tasks

import contributors.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    coroutineScope {
        val channel = Channel<List<User>>()
        val repos = service.getOrgRepos(req.org).bodyList()
        var users = mutableListOf<User>()

        for (repo in repos) {
            launch {
                val contributors = service.getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
                channel.send(contributors)
            }
        }

        repeat(repos.size) {
            users.addAll(channel.receive())
            val aggregate = users.aggregate()

            users.clear()
            users.addAll(aggregate)

            updateResults(users, it == repos.lastIndex)
        }
    }
}
