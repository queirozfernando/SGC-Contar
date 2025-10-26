# 📱 SGC-Contar

**SGC-Contar** é um aplicativo Android desenvolvido em **Kotlin + Jetpack Compose**, projetado para facilitar **contagens de estoque**, integrando captura de código de barras e registro de quantidades de forma simples, rápida e visualmente moderna.

---

## 🚀 Principais Funcionalidades

- 📦 **Contagem de Produtos**  
  Registre facilmente os itens do estoque com campos de produto, quantidade e observações.

- 📸 **Leitura de Código de Barras (CameraX + MLKit)**  
  Escaneie códigos de barras diretamente com a câmera do dispositivo, sem necessidade de hardware adicional.

- 💾 **Banco de Dados Local (Room)**  
  Todas as informações são armazenadas localmente utilizando **Room Database**, com entidades e DAOs organizadas para futuras integrações com servidor ou API.

- 🧩 **Injeção de Dependência (Hilt + Dagger)**  
  Estrutura limpa e modular, facilitando testes, manutenção e escalabilidade.

- 🧠 **Arquitetura MVVM + Compose**  
  Separação clara entre camadas de UI, lógica e dados, com **StateFlow** e **ViewModel** garantindo reatividade e performance.

---

## 🏗️ Tecnologias Utilizadas

| Componente | Descrição |
|-------------|------------|
| **Kotlin 2.0.21** | Linguagem principal |
| **Jetpack Compose 1.9+** | Interface moderna declarativa |
| **Room 2.8.3** | Banco de dados local |
| **Hilt 2.57.2** | Injeção de dependência |
| **Navigation Compose** | Navegação entre telas |
| **CameraX + MLKit** | Leitura de códigos de barras |
| **Retrofit / OkHttp / Moshi** | (Preparado para futuras integrações com APIs) |
| **WorkManager** | (Base para tarefas em segundo plano) |

---

## 🧱 Estrutura do Projeto

