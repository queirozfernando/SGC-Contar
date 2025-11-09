package com.example.myapplication.ui.contagem

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.myapplication.data.repo.ProductRepository
import com.example.myapplication.data.repo.ContagemRepository

@HiltViewModel
class ContagemViewModel @Inject constructor(
    val productRepo: ProductRepository,
    val contagemRepo: ContagemRepository
) : ViewModel()
