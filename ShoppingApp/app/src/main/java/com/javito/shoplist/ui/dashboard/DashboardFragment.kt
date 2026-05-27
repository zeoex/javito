package com.javito.shoplist.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.javito.shoplist.R
import com.javito.shoplist.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardLista.setOnClickListener {
            findNavController().navigate(R.id.nav_lista)
        }
        binding.cardIngresos.setOnClickListener {
            findNavController().navigate(R.id.nav_ingresos)
        }
        binding.cardFacturas.setOnClickListener {
            findNavController().navigate(R.id.nav_facturas)
        }
        binding.cardVarios.setOnClickListener {
            findNavController().navigate(R.id.nav_varios)
        }
        binding.cardResumen.setOnClickListener {
            findNavController().navigate(R.id.nav_resumen)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
