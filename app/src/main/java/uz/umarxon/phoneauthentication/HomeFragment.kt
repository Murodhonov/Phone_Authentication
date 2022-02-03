package uz.umarxon.phoneauthentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import uz.umarxon.phoneauthentication.databinding.FragmentHomeBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding:FragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {

        binding = FragmentHomeBinding.inflate(layoutInflater)

        binding.go.setOnClickListener {
            val number = binding.number.text.toString()
            if (number != "") {
                if (number.length == 13) {
                    findNavController().navigate(R.id.recieverFragment, bundleOf("number" to binding.number.text.toString()))
                } else {
                    Toast.makeText(context, "Telefon raqimingizni to'liq formatda kiriting", Toast.LENGTH_LONG).show()
                }
            }
        }

        return binding.root

    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onResume() {
        super.onResume()

        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null){
            findNavController().navigate(R.id.userFragment)
        }
    }

}