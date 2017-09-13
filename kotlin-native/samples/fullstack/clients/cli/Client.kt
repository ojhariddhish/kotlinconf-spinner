import kjson.*
import kliopt.*
import kurl.*
import common.*
import kotlinx.cinterop.*

fun machineName() =
    memScoped {
        val u = alloc<utsname>()
        if (uname(u.ptr) == 0) {
            "${u.sysname?.toKString()} ${u.machine?.toKString()}"
        } else {
            "unknown"
        }
    }

fun main(args: Array<String>) {
    var server: String? = null
    var name: String? = null
    var command: String? = null
    val options = listOf(
            OptionDescriptor(OptionType.STRING, "s", "server", "Server to connect", "http://localhost:1111"),
            OptionDescriptor(OptionType.STRING, "n", "name", "User name", "CLI user"),
            OptionDescriptor(OptionType.BOOLEAN, "h", "help", "Usage info"),
            OptionDescriptor(OptionType.STRING, "c", "command", "Command to issue", "stats")
    )
    parseOptions(options, args).forEach {
        when (it.descriptor.longName) {
            "server" -> server = it.stringValue
            "name" -> name = it.stringValue
            "command" -> command = it.stringValue
            "help" -> println(makeUsage(options))
        }
    }
    val machine = machineName()
    println("Connecting to $server as $name from $machine")
    KUrl("$server/json/$command?name=$name&client=cli&machine=$machine", "cookies.txt").fetch {
        content -> withJson(content) {
            println("Got $it, my color is ${it.getInt("color")}")
        }
    }
}