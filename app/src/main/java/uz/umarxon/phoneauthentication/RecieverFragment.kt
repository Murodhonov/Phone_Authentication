package uz.umarxon.phoneauthentication

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import uz.umarxon.phoneauthentication.databinding.FragmentHomeBinding
import uz.umarxon.phoneauthentication.databinding.FragmentRecieverBinding
import java.util.concurrent.TimeUnit
import android.os.CountDownTimer
import java.text.DecimalFormat
import java.text.NumberFormat


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class RecieverFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentRecieverBinding
    lateinit var auth: FirebaseAuth
    private val TAG = "FragmentReciever"
    lateinit var storedVerificationId:String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRecieverBinding.inflate(layoutInflater)

        object : CountDownTimer(50000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val f: NumberFormat = DecimalFormat("00")
                val hour = millisUntilFinished / 3600000 % 24
                val min = millisUntilFinished / 60000 % 60
                val sec = millisUntilFinished / 1000 % 60
                binding.time.text = f.format(hour).toString() + ":" + f.format(min) + ":" + f.format(sec)
            }

            override fun onFinish() {
                binding.time.text = "00:00:00"
                binding.question.visibility = View.VISIBLE
            }
        }.start()

        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RecieverFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RecieverFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()

        auth = FirebaseAuth.getInstance()

        if (arguments?.getString("number") != null){
            sendVerificationCode(arguments?.getString("number").toString())
            Toast.makeText(context, "Kod yuborilmoqda...", Toast.LENGTH_SHORT).show()
        }

        val phone = arguments?.getString("number")!!

        binding.phonetext.text = "Bir martalik kod  (+${phone[1]}${phone[2]}${phone[3]} ${phone[4]}${phone[5]}) ${phone[6]}${phone[7]}${phone[8]}-**-**\nraqamiga yuborildi"

        binding.et.addTextChangedListener{
            verifyCode()
        }

        binding.resending.setOnClickListener {
            sendVerificationCode(arguments?.getString("number").toString())
            Toast.makeText(context, "Kod qayta yuborilmoqda...", Toast.LENGTH_SHORT).show()

            binding.question.visibility = View.GONE

            object : CountDownTimer(50000, 1000) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    val f: NumberFormat = DecimalFormat("00")
                    val hour = millisUntilFinished / 3600000 % 24
                    val min = millisUntilFinished / 60000 % 60
                    val sec = millisUntilFinished / 1000 % 60
                    binding.time.text = f.format(hour).toString() + ":" + f.format(min) + ":" + f.format(sec)
                }

                override fun onFinish() {
                    binding.time.text = "00:00:00"
                    binding.question.visibility = View.VISIBLE
                }
            }.start()
        }

        binding.txt.setOnTouchListener { view, motionEvent ->

            when(motionEvent.action){
                MotionEvent.ACTION_DOWN->{
                    binding.txt.setTextColor(Color.RED)
                }
                MotionEvent.ACTION_UP->{
                    binding.txt.setTextColor(Color.GREEN)
                }
                else->{

                }
            }

            true
        }

        binding.et.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                verifyCode()
                val view = Data.mywindow
                if (view != null) {
                    val imm: InputMethodManager = binding.root.context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }else{
                    Toast.makeText(context, "null", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }

    }

    fun verifyCode(){
        val code = binding.et.text.toString()
        if (code.length == 6){
            val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
            signInWithPhoneAuthCredential(credential)
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity((activity as MainActivity))                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener((activity as MainActivity)) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user

                    findNavController().navigate(R.id.userFragment)

                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(context, "Kod xato kiritildi", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(context, "Muvaffaqiyatsiz!!!", Toast.LENGTH_SHORT).show()
                    }
                    // Update UI
                }
            }
    }

    private fun resentCode(phoneNimber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNimber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity((activity as MainActivity))                 // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .setForceResendingToken(resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}