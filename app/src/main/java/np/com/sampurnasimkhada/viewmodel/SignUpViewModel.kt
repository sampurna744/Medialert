package np.com.sampurnasimkhada.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import np.com.sampurnasimkhada.model.UserSignUp

class SignUpViewModel : ViewModel() {
    var signUpState by mutableStateOf(UserSignUp())
        private set

    fun onNameChange(name: String) {
        signUpState = signUpState.copy(name = name)
    }

    fun onEmailChange(email: String) {
        signUpState = signUpState.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        signUpState = signUpState.copy(password = password)
    }
}
