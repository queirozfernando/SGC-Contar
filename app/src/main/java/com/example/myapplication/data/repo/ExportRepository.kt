package com.example.myapplication.data.repo

import com.example.myapplication.data.local.AppDb
import com.example.myapplication.data.remote.export.ExportApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportRepository @Inject constructor(
    private val db: AppDb,
    private val api: ExportApi
) {
    // Adicione os métodos de export quando precisar
}
