package com.example.prog7313poe.ui.login

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.prog7313poe.R
import com.example.prog7313poe.databinding.ActivityMyLoginBinding

class MyLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyLoginBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        setupObservers()
        setupInputListeners()
        setupButtonListeners()
    }

    private fun setupObservers() {
        loginViewModel.loginFormState.observe(this, Observer { state ->
            state ?: return@Observer

            binding.btnLogin.isEnabled = state.isDataValid

            if (state.usernameError != null) {
                binding.etxtEmail.error = getString(state.usernameError)
            }

            if (state.passwordError != null) {
                binding.etxtPassword.error = getString(state.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this, Observer { result ->
            result ?: return@Observer

            binding.loading.visibility = View.GONE

            if (result.error != null) {
                showLoginFailed(result.error)
            }

            result.success?.let {
                updateUiWithUser(it)
                setResult(Activity.RESULT_OK)
                finish()
            }
        })
    }

    private fun setupInputListeners() {
        binding.etxtEmail.afterTextChanged {
            loginViewModel.loginDataChanged(
                binding.etxtEmail.text.toString(),
                binding.etxtPassword.text.toString()
            )
        }

        binding.etxtPassword.afterTextChanged {
            loginViewModel.loginDataChanged(
                binding.etxtEmail.text.toString(),
                binding.etxtPassword.text.toString()
            )
        }

        binding.etxtPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performLogin()
                true
            } else {
                false
            }
        }
    }

    private fun setupButtonListeners() {
        binding.btnLogin.setOnClickListener {
            binding.loading.visibility = View.VISIBLE
            performLogin()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun performLogin() {
        loginViewModel.login(
            binding.etxtEmail.text.toString(),
            binding.etxtPassword.text.toString()
        )
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        Toast.makeText(applicationContext, "$welcome ${model.displayName}", Toast.LENGTH_LONG).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, getString(errorString), Toast.LENGTH_SHORT).show()
    }
}

// Extension function to simplify setting an afterTextChanged action to EditText components.
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : android.text.TextWatcher {
        override fun afterTextChanged(editable: android.text.Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}
