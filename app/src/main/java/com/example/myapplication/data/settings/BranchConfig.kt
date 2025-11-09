package com.example.myapplication.data.settings

import kotlinx.serialization.Serializable   // ✅ ESSA é a importação certa

@Serializable
data class BranchConfig(
    val id: String,
    val nome: String,
    val backendUrl: String,   // URL do backend: "http://192.168.15.11:8000/"
    val dbServer: String,     // host/IP do servidor de dados
    val dbName: String,       // nome do banco de dados
    val apiToken: String = "" // opcional
)
