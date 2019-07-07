package com.sliebald.pairshare

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.sliebald.pairshare.databinding.ActivityMainBinding
import com.sliebald.pairshare.utils.ExpenseListUtils
import com.sliebald.pairshare.utils.KeyboardUtils
import com.sliebald.pairshare.utils.PreferenceUtils
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private val mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private val mViewModel: MainActivityViewModel by lazy {
        ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
    }

    private lateinit var mBinding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // setContentView(R.layout.activity_main);

        //Log user in if he isn't already logged in.
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                val providers = listOf(AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build())
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(!BuildConfig.DEBUG, true)
                                .setAvailableProviders(providers).build(),
                        RC_SIGN_IN)
            }
        }
        // set addEntry and summary as tld destinations --> no back button
        val host: NavHostFragment = supportFragmentManager
                .findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment? ?: return
        val navController = host.navController



        appBarConfiguration = AppBarConfiguration.Builder(setOf(R.id.addExpense_dest, R.id
                .overviewExpenses_dest, R.id.selectExpenseList_dest))
                .build()
        setupActionBar(navController, appBarConfiguration)
        setupBottomNavMenu(navController)

        // Make some extra checks which destination changes are currently allowed and handle
        // bottom navigation visibility.
        navController.addOnDestinationChangedListener { controller, destination, _ ->
            //if no expenselist is selected, let them select or add one
            if (PreferenceUtils.selectedSharedExpenseListID.isEmpty()) {
                mBinding.bottomNavView.visibility = View.GONE
                if (!(destination.id == R.id.selectExpenseList_dest || destination.id == R.id.addExpenseList_dest)) {
                    Snackbar.make(mBinding.mainLayout,
                            getString(R.string.warning_add_select_list),
                            Snackbar.LENGTH_LONG).show()
                    controller.navigate(R.id.selectExpenseList_dest)

                }
            } else {
                if (destination.id == R.id.about_dest || destination.id == R.id.addExpenseList_dest) {
                    mBinding.bottomNavView.visibility = View.GONE
                } else {
                    mBinding.bottomNavView.visibility = View.VISIBLE
                }
            }
        }


        //Get the current expense diff for the subtitle in all fragments

        mViewModel.activeExpenseList.observe(this, Observer { expenseList ->
            val expenseDiff = ExpenseListUtils.getExpenseDifferenceFor(mFirebaseAuth.uid!!,
                    expenseList)

            val title = expenseList.listName!! + ": "
            val completeSummaryString = title + String.format(Locale.GERMAN, "%.2f€", expenseDiff)

            val spannable = SpannableString(completeSummaryString)

            spannable.setSpan(ForegroundColorSpan(ExpenseListUtils.getExpenseDifferenceColor(expenseDiff)), title.length,
                    completeSummaryString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            mBinding.toolbar.subtitle = spannable
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (response == null) {
                //TODO: according to the documentation this should happen if back is pressed in
                // the login activity. But it doesn't seem to work (emulator)
                // possible that a new login form is started before this is called and then
                // finish is called once a login is successful
                finish()
            }

            if (resultCode == RESULT_OK) {
                mViewModel.userLoggedIn()
            }


        }
    }


    /**
     * Setup the bottom navigation with the navController.
     *
     * @param navController The navController for the navigation mechanism
     */
    private fun setupBottomNavMenu(navController: NavController) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNav?.setupWithNavController(navController)
        // Hide the bottom navigation if the keyboard is open.
        KeyboardVisibilityEvent.setEventListener(
                this
        ) { isOpen ->
            if (isOpen) {
                bottomNav.visibility = View.GONE
            } else {
                bottomNav.visibility = View.VISIBLE
            }
        }

    }

    /**
     * Setup the actionbar with the navController and toolbar.
     *
     * @param navController The navController for the navigation mechanism
     */
    private fun setupActionBar(navController: NavController, appBarConfiguration: AppBarConfiguration) {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        KeyboardUtils.hideKeyboard(this, mBinding.root)
        return findNavController(R.id.my_nav_host_fragment).navigateUp(appBarConfiguration)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                AuthUI.getInstance().signOut(this)
                true
            }
            else -> {
                (item.onNavDestinationSelected(findNavController(R.id.my_nav_host_fragment))
                        || super.onOptionsItemSelected(item))
            }
        }

    }

    override fun onStart() {
        super.onStart()
        mFirebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        mFirebaseAuth.removeAuthStateListener(authStateListener)
    }

    companion object {
        /**
         * Result key for sign in activity.
         */
        private const val RC_SIGN_IN = 53252
    }


}


