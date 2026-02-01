# CheckMarket 🛒

[![Kotlin Version](https://img.shields.io/badge/Kotlin-1.9+-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Android SDK](https://img.shields.io/badge/Android%20SDK-24%20--%2034-green.svg?style=flat&logo=android)](https://developer.android.com/)
[![Firebase](https://img.shields.io/badge/Backend-Firebase%20Firestore-orange.svg?style=flat&logo=firebase)](https://firebase.google.com/)

O **CheckMarket** é um assistente inteligente de compras para Android. Desenvolvido de forma nativa em Kotlin, permite gerir listas de compras dinâmicas com sincronização imediata via Cloud, garantindo que nunca perdes o controlo dos teus gastos e inventário.

---

## ✨ Funcionalidades Principais

* **⚡ Sincronização Cloud:** Integração nativa com Firebase Firestore para persistência de dados em tempo real.
* **🖱️ Gestos Intuitivos (UX):** * *Swipe Left:* Eliminação rápida de produtos.
    * *Swipe Right:* Marcar itens como comprados/concluídos.
* **🔍 Filtros e Pesquisa:** Barra de pesquisa em tempo real e sistema de chips para filtragem por categorias ou estado.
* **📊 Gestão de Preços:** Cálculo automático de subtotais e totais baseados em quantidade e preço unitário.
* **🔙 Recuperação de Dados:** Sistema de "Desfazer" (Undo) via SnackBar ao eliminar um item por acidente.

## 🛠️ Stack Tecnológica

* **Linguagem:** [Kotlin](https://kotlinlang.org/)
* **UI/Layout:** Material Design 3 (Componentes modernos, Chips, FAB, CardViews).
* **Arquitetura:** View Binding (para um código mais limpo e seguro contra NullPointerExceptions).
* **Base de Dados:** Google Firebase Firestore (NoSQL).
* **Gestão de Imagens/Recursos:** Vetores otimizados (XML) para garantir leveza na APK.

## 📦 Estrutura do Projeto

```text
app/src/main/java/com/example/checkmarket/
├── MainActivity.kt        # Gestão da lista principal e lógica do Firestore
├── FormularioActivity.kt  # Interface de CRUD (Criar/Editar produtos)
├── Produto.kt             # Data Class (Modelo de dados serializável)
└── ProdutoAdapter.kt      # Lógica de renderização e animações da lista
```
## **🚀 Como Executar**

* **Clonar o projeto:**
    ```bash
    git clone [https://github.com/RubenMRS/CheckMarket.git](https://github.com/RubenMRS/CheckMarket.git)
    ```

* **Configuração do Firebase:**
    * Cria um projeto no [Console do Firebase](https://console.firebase.google.com/).
    * Descarrega o ficheiro `google-services.json` e coloca-o na diretoria `app/`.

* **Android Studio:**
    * Abre o projeto e aguarda a sincronização do **Gradle**.
    * Executa no teu dispositivo ou emulador (**Min SDK 24**).

Autor: Ruben Silva
