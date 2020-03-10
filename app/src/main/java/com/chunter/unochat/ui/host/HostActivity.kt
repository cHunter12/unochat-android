package com.chunter.unochat.ui.host

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.chunter.unochat.R
import com.crashlytics.android.Crashlytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HostActivity : AppCompatActivity(R.layout.activity_host) {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private lateinit var navController: NavController
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navController = findNavController(R.id.hostFragment)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setupWithNavController(navController, AppBarConfiguration(setOf(R.id.homeFragment)))

        if (firebaseAuth.currentUser != null) {
            navController.navigate(R.id.navToHome)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                lifecycleScope.launch {
                    val token = FirebaseInstanceId.getInstance().instanceId
                        .await()
                        .token

                    Firebase.firestore.document("users/${firebaseAuth.currentUser!!.uid}")
                        .update("token", token)
                        .await()

                    firebaseAuth.signOut()

                    withContext(Dispatchers.Main) {
                        navController.navigate(R.id.navToLogin)
                    }
                }
            }
            R.id.crash -> Crashlytics.getInstance().crash()
        }
        return super.onOptionsItemSelected(item)
    }
}
