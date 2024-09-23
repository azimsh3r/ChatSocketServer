package network

import kotlinx.coroutines.*
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket
import kotlin.Exception
import kotlin.random.Random

class SocketManager {
    fun run() {
        val registrationSocketServer = ServerSocket(1111)
        println("Server launched!")

        var socket : Socket? = null

        var outputStreamWriter: OutputStreamWriter? = null
        var bufferedWriter : BufferedWriter? = null

        while (true) {
            try {
                socket = registrationSocketServer.accept()

                outputStreamWriter = OutputStreamWriter(socket.getOutputStream())
                bufferedWriter = BufferedWriter(outputStreamWriter)

                val newPortNumber = createServerSocket()

                bufferedWriter.write(newPortNumber.toString())
                bufferedWriter.flush()
            } catch (e : Exception) {
                e.printStackTrace()
            } finally {
                socket?.close()
                outputStreamWriter?.close()
                bufferedWriter?.close()
            }
        }
    }

    private fun createServerSocket() : Int {
        val portNumber = findRandomAvailablePort()
        val serverSocket = ServerSocket(portNumber)
        val chatSession = ChatSession()

        CoroutineScope(Dispatchers.Default).launch {
            chatSession.registerSocketClients(serverSocket)
        }
        println("Port Number of the chat: $portNumber")
        return portNumber
    }

    private fun findRandomAvailablePort() : Int {
        var port: Int

        while (true) {
            port = Random.nextInt(49152, 65535)
            try {
                ServerSocket(3).use {
                    return port
                }
            } catch (e: Exception) {
                println("This port is being used! $port")
            }
        }
    }
}
