package np.com.sampurnasimkhada.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import np.com.sampurnasimkhada.model.UserSignIn

class SignInViewModel : ViewModel() {
    var signInState by mutableStateOf(UserSignIn())
        private set

    fun onEmailChange(email: String) {
        signInState = signInState.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        signInState = signInState.copy(password = password)
    }
}
