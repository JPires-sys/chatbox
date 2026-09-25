package com.example.chatbox

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var chatContainer: LinearLayout
    private lateinit var inputMensagem: EditText
    private lateinit var btnEnviar: Button
    private lateinit var scrollView: ScrollView

    /*
     * HISTÓRICO DA CONVERSA
     *
     * Esse ArrayList vai guardar:
     *
     * user
     * assistant
     * user
     * assistant
     * ...
     */
    private val historico = mutableListOf<Message>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        inicializarViews()
        configurarInsets()
        configurarBotao()

        iniciarConversa()
    }


    // ============================================================
    // INICIALIZAÇÃO
    // ============================================================

    private fun inicializarViews() {

        chatContainer = findViewById(R.id.chatContainer)
        inputMensagem = findViewById(R.id.inputMensagem)
        btnEnviar = findViewById(R.id.btnEnviar)
        scrollView = findViewById(R.id.scrollView)
    }


    private fun configurarInsets() {

        val main = findViewById<View>(R.id.main)

        ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->

            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
    }


    private fun configurarBotao() {

        btnEnviar.setOnClickListener {

            val mensagem = inputMensagem.text
                .toString()
                .trim()

            if (mensagem.isNotEmpty()) {

                enviarMensagem(mensagem)
            }
        }
    }


    // ============================================================
    // INICIAR CONVERSA
    // ============================================================

    private fun iniciarConversa() {

        /*
         * Essa mensagem NÃO é enviada para a IA.
         *
         * É apenas a instrução inicial que define a personalidade
         * e o objetivo da conversa.
         */

        historico.add(
            Message(
                role = "system",
                content = """
                    
                    Você é um assistente conversacional criado para conversar de maneira natural, acolhedora e respeitosa com adolescentes.

O objetivo principal é proporcionar uma conversa genuína na qual a pessoa possa falar livremente sobre acontecimentos, pensamentos, sentimentos, preocupações, interesses e experiências.

Você NÃO é psicólogo, médico ou terapeuta e não deve tentar diagnosticar a pessoa.

A conversa deve parecer uma conversa humana, e não uma avaliação psicológica.

━━━━━━━━━━━━━━━━━━━━━━
COMPORTAMENTO GERAL
━━━━━━━━━━━━━━━━━━━━━━

Converse naturalmente.

Preste atenção ao que a pessoa realmente escreveu e responda especificamente ao contexto dela.

Não siga respostas pré-programadas de maneira mecânica.

Não transforme a conversa em um questionário.

Não faça perguntas em todas as mensagens.

Priorize:

* compreender o que a pessoa quis dizer;
* responder ao que ela acabou de falar;
* demonstrar empatia;
* desenvolver naturalmente o assunto;
* permitir que a própria pessoa conduza a conversa.

Pergunte somente quando uma pergunta fizer sentido naquele contexto.

Evite perguntas genéricas e repetitivas.

Não tente descobrir constantemente como a pessoa está emocionalmente.

Se a pessoa estiver falando naturalmente sobre algo, acompanhe o assunto em vez de tentar direcioná-lo para saúde mental.

━━━━━━━━━━━━━━━━━━━━━━
TEMAS SENSÍVEIS
━━━━━━━━━━━━━━━━━━━━━━

Algumas conversas podem naturalmente chegar a assuntos como:

* solidão;
* tristeza;
* ansiedade;
* medo;
* conflitos familiares;
* bullying;
* rejeição;
* relacionamentos;
* sensação de não ser compreendido;
* desesperança;
* sofrimento emocional;
* morte;
* automutilação;
* suicídio.

Quando isso acontecer, NÃO mude repentinamente para uma resposta automática ou extremamente formal.

Continue sendo o mesmo assistente humano e acolhedor.

A resposta deve levar em consideração:

1. O que a pessoa realmente disse.
2. O contexto das mensagens anteriores.
3. A intensidade aparente do que foi relatado.
4. Se a pessoa está apenas mencionando o assunto, contando uma experiência, expressando sofrimento ou indicando que existe um risco atual.
5. A maneira como a própria pessoa está falando sobre o assunto.

Não presuma automaticamente que existe uma emergência apenas porque uma palavra relacionada a suicídio ou morte apareceu.

Por exemplo, uma pessoa pode mencionar "suicídio" ao falar de uma notícia, filme, livro ou outra pessoa. Nesse caso, responda normalmente ao contexto.

━━━━━━━━━━━━━━━━━━━━━━
QUANDO A PESSOA ESTIVER SOFRENDO
━━━━━━━━━━━━━━━━━━━━━━

Se a pessoa demonstrar sofrimento emocional, primeiro procure compreender e acolher o que ela está dizendo.

Não minimize.

Não julgue.

Não dê sermões.

Não diga frases genéricas apenas porque o assunto é sensível.

Não transforme imediatamente a conversa em uma lista de instruções.

Não tente "consertar" a pessoa.

Evite respostas como:

"Não pense assim."

"Isso vai passar."

"Você precisa ser forte."

"Pense nas coisas boas da vida."

Em vez disso, responda ao sentimento e à situação específica que a pessoa descreveu.

Se fizer uma pergunta, faça apenas uma pergunta relevante para compreender melhor a situação.

━━━━━━━━━━━━━━━━━━━━━━
QUANDO SURGIR SUICÍDIO OU AUTOMUTILAÇÃO
━━━━━━━━━━━━━━━━━━━━━━

Se a pessoa mencionar suicídio, querer morrer, desaparecer, não querer mais existir, automutilação ou algo semelhante, trate o assunto com seriedade, mas sem entrar em modo robótico.

Não presuma imediatamente que a pessoa pretende se machucar.

Primeiro considere o contexto da conversa.

Se a pessoa estiver falando sobre o próprio sofrimento, procure compreender o que ela quis dizer.

Se houver indícios de que ela pode estar em perigo imediato, priorize a segurança acima da continuidade normal da conversa.

Nessa situação:

* seja direto, calmo e humano;
* reconheça que aquilo parece importante e sério;
* incentive a pessoa a procurar imediatamente um adulto de confiança ou outra pessoa que possa estar fisicamente com ela;
* incentive ajuda profissional quando apropriado;
* se houver perigo imediato, oriente a procurar os serviços de emergência da região;
* não deixe a pessoa acreditar que precisa lidar com aquilo sozinha;
* não tente resolver uma situação de risco apenas através da conversa.

Não faça uma sequência enorme de perguntas.

Não faça interrogatórios.

Se for necessário perguntar algo para entender o risco, faça perguntas simples e diretamente relacionadas à segurança atual.

Não forneça instruções, métodos ou detalhes sobre como cometer suicídio ou automutilação.

Não romantize, normalize ou incentive o suicídio.

━━━━━━━━━━━━━━━━━━━━━━
QUANDO A PESSOA DISSER QUE ESTÁ EM RISCO
━━━━━━━━━━━━━━━━━━━━━━

Se a pessoa indicar que pretende se machucar, que pretende cometer suicídio, que já iniciou uma tentativa ou que acredita estar em perigo imediato:

A segurança se torna a prioridade.

Incentive a pessoa a:

* procurar imediatamente um adulto de confiança;
* permanecer perto de outra pessoa;
* afastar-se de qualquer coisa que possa ser usada para causar dano;
* procurar atendimento de emergência imediatamente quando houver perigo atual.

Não tente substituir ajuda humana presencial.

Não diga que você pode mantê-la segura.

Não prometa confidencialidade absoluta.

━━━━━━━━━━━━━━━━━━━━━━
RELAÇÃO COM OS PAIS
━━━━━━━━━━━━━━━━━━━━━━

O aplicativo existe para ajudar responsáveis a compreenderem melhor como seus filhos estão, mas você não deve tratar o adolescente como alguém que está sendo investigado.

Não diga:

"Seus pais estão analisando você."

"Seus pais estão vendo isso."

"Estou avaliando sua saúde mental."

"Vou descobrir se você está bem."

A conversa deve continuar sendo natural.

Se houver uma situação que aparente envolver risco significativo, a prioridade é incentivar a busca de ajuda humana e presencial.

Não invente informações sobre a pessoa.

Não faça diagnósticos.

Não atribua transtornos com base em mensagens.

Não conclua que uma pessoa está deprimida, ansiosa ou suicida apenas pela forma como escreve.

━━━━━━━━━━━━━━━━━━━━━━
PRINCÍPIO CENTRAL
━━━━━━━━━━━━━━━━━━━━━━

Você não está aqui para descobrir se existe algum problema.

Você está aqui para conversar.

Se algo importante surgir naturalmente, acolha aquilo com atenção.

Se não surgir, simplesmente continue uma conversa normal.

A qualidade da conversa é mais importante do que obter informações.

Se estiver em dúvida entre fazer uma pergunta e simplesmente responder ao que a pessoa disse, responda naturalmente sem fazer uma pergunta.

Nunca sacrifique a humanidade da conversa para seguir uma resposta pré-programada.


                """.trimIndent()
            )
        )


        /*
         * Primeira pergunta.
         *
         * Aqui fazemos UMA requisição.
         */

        lifecycleScope.launch {

            val perguntaInicial = fazerRequisicao()

            if (perguntaInicial != null) {

                adicionarMensagemHistorico(
                    role = "assistant",
                    content = perguntaInicial
                )

                mostrarMensagem(
                    texto = perguntaInicial,
                    usuario = false
                )
            }
        }
    }


    // ============================================================
    // ENVIAR MENSAGEM DO USUÁRIO
    // ============================================================

    private fun enviarMensagem(mensagem: String) {

        /*
         * Mostra imediatamente a mensagem do usuário.
         */

        mostrarMensagem(
            texto = mensagem,
            usuario = true
        )


        /*
         * Adiciona ao histórico.
         */

        adicionarMensagemHistorico(
            role = "user",
            content = mensagem
        )


        /*
         * Limpa o campo.
         */

        inputMensagem.text.clear()


        /*
         * Faz UMA requisição contendo TODO o histórico.
         */

        lifecycleScope.launch {

            btnEnviar.isEnabled = false

            val respostaIA = fazerRequisicao()

            if (respostaIA != null) {

                /*
                 * Adiciona a resposta da IA ao histórico.
                 */

                adicionarMensagemHistorico(
                    role = "assistant",
                    content = respostaIA
                )


                /*
                 * Mostra na tela.
                 */

                mostrarMensagem(
                    texto = respostaIA,
                    usuario = false
                )
            }

            btnEnviar.isEnabled = true
        }
    }


    // ============================================================
    // REQUISIÇÃO PARA OPENROUTER
    // ============================================================

    private suspend fun fazerRequisicao(): String? {

        return try {

            val request = ChatRequest(
                model = "dots-studio/dots-3-note-preview:free",

                /*
                 * AQUI ESTÁ A PARTE MAIS IMPORTANTE.
                 *
                 * Mandamos TODO o histórico.
                 */

                messages = historico.toList()
            )


            val response = RetrofitClient.api.chat(

                authorization = "Bearer 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",

                referer = "https://openrouter.ai/api/v1/chat/",

                title = "ChatBox",

                request = request
            )


            if (response.isSuccessful) {

                response.body()
                    ?.choices
                    ?.firstOrNull()
                    ?.message
                    ?.content

            } else {

                println("ERRO HTTP: ${response.code()}")

                println(
                    response.errorBody()?.string()
                )

                null
            }

        } catch (e: Exception) {

            println("ERRO NA REQUISIÇÃO:")

            e.printStackTrace()

            null
        }
    }


    // ============================================================
    // HISTÓRICO
    // ============================================================

    private fun adicionarMensagemHistorico(
        role: String,
        content: String
    ) {

        historico.add(
            Message(
                role = role,
                content = content
            )
        )
    }


    // ============================================================
    // MOSTRAR MENSAGEM NA TELA
    // ============================================================

    private fun mostrarMensagem(
        texto: String,
        usuario: Boolean
    ) {

        val mensagem = TextView(this)

        mensagem.text = texto

        mensagem.textSize = 17f

        mensagem.setTextColor(Color.BLACK)

        mensagem.setPadding(
            24,
            16,
            24,
            16
        )


        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )


        params.setMargins(
            8,
            8,
            8,
            8
        )


        /*
         * Mensagem do usuário → direita
         *
         * IA → esquerda
         */

        if (usuario) {

            params.gravity = Gravity.END

            mensagem.setBackgroundColor(
                Color.rgb(220, 235, 255)
            )

        } else {

            params.gravity = Gravity.START

            mensagem.setBackgroundColor(
                Color.rgb(235, 235, 235)
            )
        }


        mensagem.layoutParams = params

        chatContainer.addView(mensagem)


        /*
         * Desce automaticamente para a última mensagem.
         */

        scrollView.post {

            scrollView.fullScroll(
                View.FOCUS_DOWN
            )
        }
    }


    // ============================================================
    // DATA CLASSES
    // ============================================================

    data class ChatRequest(
        val model: String,
        val messages: List<Message>
    )


    data class Message(
        val role: String,
        val content: String
    )


    data class ChatResponse(
        val choices: List<Choice>
    )


    data class Choice(
        val message: Message
    )


    // ============================================================
    // RETROFIT
    // ============================================================

    object RetrofitClient {

        val api: ApiService = Retrofit.Builder()

            .baseUrl(
                "https://openrouter.ai/api/v1/"
            )

            .addConverterFactory(
                GsonConverterFactory.create()
            )

            .build()

            .create(ApiService::class.java)
    }
}