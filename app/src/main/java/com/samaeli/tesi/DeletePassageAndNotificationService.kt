package com.samaeli.tesi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.samaeli.tesi.Passages.MyPassageSummaryActivity
import com.samaeli.tesi.models.Offer
import com.samaeli.tesi.models.Passage
import java.util.*

// Servizio in background che ha il compito di cancellare un passaggio se l'ora attuale supera l'ora a cui è stato richiesto
// Il servizio inoltre ha il compito di inviare le notifiche
class DeletePassageAndNotificationService : Service() {

    var timerDeletePassage : Timer? = null

    companion object{
        val TAG = "DeletePassageAndNotificationService"
        val nameChannel = "Notification Title"
        val descriptionTextChannel = "NotificationDescription"
        val channelId = "ChannelId"

        const val ACCEPT = "accept"
        const val DELETE = "delete"
        const val WAIT = "wait"
        const val WITHDRAW = "withdraw"
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG,"onBind Not used")
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG,"Service created")
        createNotificationChannel()
        startTimerDeletePassage()

        checkDeleteOffers()
        checkAcceptedOffers()
        checkWaitOffers()
        checkWithdrawOffers()
    }

    override fun onDestroy() {
        Log.d(TAG,"Service destroyed")
        stopTimerDeletePassage()
        super.onDestroy()
    }

    // Metodo che ha il compito di far partire il timer per la cancellazione dei passaggi che hanno
    // un'ora che supera l'ora attuale
    private fun startTimerDeletePassage(){
        timerDeletePassage = Timer(true)
        timerDeletePassage!!.schedule(object : TimerTask(){
            override fun run() {
                Log.d(TAG,"RUN")
                checkPassage()
            }
        },0,1000 * 60 *3) // Ogni 3 minuti
    }

    private fun stopTimerDeletePassage(){
        if(timerDeletePassage!=null){
            timerDeletePassage!!.cancel()
        }
    }

    // Metodo che ha il compito di controllare se ci sono delle offerte in "delete_offers/uid_utente".
    // Se ci sono si manda la notifica che una sua offerta è stata declinata e si cancellano le offerte presenti
    // in quella sezione del db
    private fun checkDeleteOffers(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("delete_offers/$uid")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                sendNotification(DELETE)
                ref.removeValue()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    // Metodo che ha il compito di inviare la notifica
    private fun sendNotification(type:String){
        val notificationIntent = Intent(applicationContext,MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val flags = PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(applicationContext,0,notificationIntent,flags)
        val icon = R.drawable.ic_launcher_foreground
        val contentTitle = getString(R.string.app_name)

        var contextText = ""
        if(type.equals(DELETE)) {
            contextText = getString(R.string.offer_declined_notification)
        }else{
            if(type.equals(ACCEPT)){
                contextText = getString(R.string.offer_accepted_notification)
            }else
            {
                if(type.equals(WAIT)) {
                    contextText = getString(R.string.offer_wait_notification)
                }else{
                    contextText = getString(R.string.offer_withdraw_notification)
                }
            }
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(icon)
                .setContentTitle(contentTitle)
                .setContentText(contextText)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
        with(NotificationManagerCompat.from(applicationContext)){
            notify(101,notification.build())
        }
        Log.d(TAG,"Notifica inviata")
    }

    // Metodo che ha il compito di controllare se ci sono delle offerte in "accepted_offers/uid_utente".
    // Se ci sono si manda la notifica che una sua offerta è stata accettata e si cancella l'offerta presenti
    // in quella sezione del db
    private fun checkAcceptedOffers(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("accepted_offers/$uid")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                sendNotification(ACCEPT)
                ref.removeValue()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    // Metodo che ha il compito di controllare se ci sono delle offerte in "wait_offers/uid_utente".
    // Se ci sono si manda la notifica che una sua offerta è stata declinata e si cancellano le offerte presenti
    // in quella sezione del db
    private fun checkWaitOffers(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("wait_offers/$uid")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                sendNotification(WAIT)
                ref.removeValue()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    // Metodo che ha il compito di controllare se ci sono delle offerte in "withdraw_offers/uid_utente"
    // Se ci sono si manda la notifica un'offerta fatta è stata ritirata e si cancellano le offerte presenti
    // in quella sezione del db
    private fun checkWithdrawOffers(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("withdraw_offers/$uid")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                sendNotification(WITHDRAW)
                ref.removeValue()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    // Metodo che ha il compito di creare il canale per le notifiche
    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId,nameChannel,importance).apply {
                description = descriptionTextChannel
            }
            val notificationManager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Metodo che ha il compito di controllare che il passaggio richiesto dall'utente abbia un orario che
    // non superi l'ora attuale. Se così fosse il passaggio viene cancellato
    private fun checkPassage(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("passages/$uid")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val passage = snapshot.getValue(Passage::class.java)
                    // Ora passaggio
                    val timeMinute: Int = passage!!.hour * 60 + passage.minute
                    // Ora attuale
                    val nowMinute: Int = Calendar.getInstance().get(Calendar.MINUTE) +
                            (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60)
                    Log.d(TAG, "Time minute: $timeMinute ; Now minute: $nowMinute")
                    // Se sono passate le 17 e l'ora del passaggio è entro le 8 si può fare se no no
                    if(!(timeMinute<8*60 && nowMinute>16*60)) {
                        if (nowMinute > timeMinute) {
                            ref.removeValue()
                            declineAllOffers()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    // Metodo che ha il compito di declinare tutte le offerte arrivate per un passaggio che viene
    // cancellato
    fun declineAllOffers(){
        Log.d(TAG, "Try to delete offers")
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("received_offers/$uid/")
        ref.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val offer = it.getValue(Offer::class.java)
                    val ref2 = FirebaseDatabase.getInstance().getReference("received_offers/$uid/${offer!!.uidBidder}")
                    val ref3 = FirebaseDatabase.getInstance().getReference("made_offers/${offer!!.uidBidder}/$uid")
                    ref3.removeValue()
                            .addOnSuccessListener {
                                Log.d(TAG,"Offerta cancellata da made_offers")
                                if(offer.state.equals(Offer.WAIT)) {
                                    addDeclinedOffer(offer!!.uidBidder, offer)
                                }
                            }
                    ref2.removeValue()
                            .addOnSuccessListener {
                                Log.d(TAG,"Offerta cancellata da receive_offers")
                            }
                }
                ref.removeEventListener(this)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    // Metodo che ha il compito di aggiungere l'offerta declinata in "delete_offers"
    private fun addDeclinedOffer(uidBidder:String,offer: Offer){
        Log.d(TAG,"DeclineOffer")
        val ref = FirebaseDatabase.getInstance().getReference("delete_offers/$uidBidder")
        offer.state = Offer.DECLINED
        ref.setValue(offer)
                .addOnFailureListener {
                    Log.d(MyPassageSummaryActivity.TAG,"Error during decline offer")
                }

    }
}