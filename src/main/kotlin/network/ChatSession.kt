package network

import dto.MessageDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors

class ChatSession {
    private var socketSet : CopyOnWriteArraySet<Socket> = CopyOnWriteArraySet()

    fun registerSocketClients(serverSocket: ServerSocket) {
        while (true) {
            var socket: Socket

            while (true) {
                try {
                    socket = serverSocket.accept()

                    CoroutineScope(Dispatchers.IO).launch {
                        handleIncomingMessages(socket)
                    }
                    socketSet.add(socket)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun handleIncomingMessages(socket: Socket) {
        println("I am handling incoming messages")
        var inputStreamReader : InputStreamReader? = null
        var bufferedReader : BufferedReader? = null

        try {
            inputStreamReader = InputStreamReader(socket.getInputStream())
            bufferedReader = BufferedReader(inputStreamReader)
            var messageJson: String

            while (true) {
                println("i am before reading")
                messageJson = bufferedReader.readLine()
                println("message received")

                val message : MessageDTO = Json.decodeFromString(messageJson)
                println("Message received in socket")

                if (message.message == "DISCONNECT") {
                    break
                }

                CoroutineScope(Dispatchers.IO).launch {
                    sendMessage(messageJson, socket)
                }
            }
        } catch (e : Exception) {
            e.printStackTrace()
        } finally {
            inputStreamReader?.close()
            bufferedReader?.close()
        }
    }

    private fun sendMessage(message: String, originalSocket: Socket) {
        var outputStreamWriter: OutputStreamWriter? = null
        var bufferedWriter : BufferedWriter? = null

        val receiverSet : MutableSet<Socket> = socketSet.toMutableSet()
        receiverSet.remove(originalSocket)

        val threadPool = Executors.newCachedThreadPool()

        for (receiver in receiverSet) {
            threadPool.submit {
                try {
                    outputStreamWriter = OutputStreamWriter(receiver.getOutputStream())
                    bufferedWriter = outputStreamWriter?.let { BufferedWriter(it) }

                    bufferedWriter?.write(message + "\n")
                    bufferedWriter?.flush()
                } catch (e : Exception) {
                    e.printStackTrace()
                }
            }
        }
        threadPool.shutdown()
    }
}
