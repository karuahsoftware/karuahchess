package purpletreesoftware.karuahchess.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.*
import kotlin.collections.ArrayList


@ExperimentalUnsignedTypes
class VoiceRecognition (pContext : Context) : RecognitionListener  {
    var voiceRecognitionListener: OnVoiceRecognitionInteractionListener? = null
    var speechRecogniser: SpeechRecognizer? = null
    val speechRecogniserIntent : Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    val context : Context = pContext
    var listening : Boolean = false

    init {
        speechRecogniserIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecogniserIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH)
        speechRecogniserIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1)

    }

    override fun onReadyForSpeech(params: Bundle?) {
        voiceRecognitionListener?.onVoiceRecogntionActive()
    }

    override fun onRmsChanged(rmsdB: Float) {

    }

    override fun onBufferReceived(buffer: ByteArray?) {

    }

    override fun onPartialResults(partialResults: Bundle?) {

    }

    override fun onEvent(eventType: Int, params: Bundle?) {

    }

    override fun onBeginningOfSpeech() {

    }

    override fun onEndOfSpeech() {
        listening = false
        voiceRecognitionListener?.onVoiceRecogntionInActive()
    }

    override fun onError(error: Int) {
        stop()
        destroySpeechRecogniser()
    }

    override fun onResults(results: Bundle?) {
        val textArrayList: ArrayList<String> = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: ArrayList()

        var cmdFound = false
        for (i in 0 until textArrayList.size) {
            val voicecmd = applyGrammar(textArrayList[i])

            if (voicecmd != "") {
                voiceRecognitionListener?.onVoiceCommandAction(voicecmd)
                cmdFound = true
                break
            }
        }

        if (!cmdFound) {
            voiceRecognitionListener?.onVoiceCommandAction("unrecognised")
        }


    }

    /**
     * Start listening
     */
    fun start() {
        if(!listening) {
            listening = true
            if(speechRecogniser == null && SpeechRecognizer.isRecognitionAvailable(context)) {
               speechRecogniser = SpeechRecognizer.createSpeechRecognizer(context)
               speechRecogniser?.setRecognitionListener(this)
            }
            speechRecogniser?.startListening(speechRecogniserIntent)
        }
    }

    /**
     * Stop listening
     */
    fun stop() {
        speechRecogniser?.stopListening()
        voiceRecognitionListener?.onVoiceRecogntionInActive()
        listening = false
    }

    /**
     * Destroys the speech object
     */
    private fun destroySpeechRecogniser() {
        speechRecogniser?.cancel()
        speechRecogniser?.destroy()
        speechRecogniser = null
    }

    /**
     * Apply grammar rules to restrict limit possible words detected
     */
    private fun applyGrammar(pSpeechText: String) : String {
        val str = when(pSpeechText.toLowerCase(Locale.ENGLISH)) {
            "help" -> "help"
            "new game", "new", "u", "news" -> "new"
            "resign" -> "resign"
            "undo" -> "undo"
            "edit" -> "edit"
            else -> ""
        }
        return str
    }

    /**
     * Set voice interaction listener
     */
    fun setVoiceRecognitionInteractionListener(pEventListener: OnVoiceRecognitionInteractionListener) {
        voiceRecognitionListener = pEventListener
    }

    /**
     * Voice recognition events
     */
    interface OnVoiceRecognitionInteractionListener {
        fun onVoiceCommandAction(pCmdText: String)
        fun onVoiceRecogntionActive()
        fun onVoiceRecogntionInActive()
    }

}