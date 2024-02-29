package com.kelmer.android.fabmenu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.kelmer.android.fabmenu.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private var revealed: Boolean = false
    
    private val binding : ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            fab1.setOnClickListener {
                menuRed.showProgressBar()
            }

            fab2.setOnClickListener {
                menuRed.hideProgress()
            }


            fab3.setOnClickListener {

            }

            menuRed.setClosedOnTouchOutside(true)
            val listener = object : MenuInterface {
                override fun menuOpen() {
                    Toast.makeText(applicationContext, "Menu opened!", Toast.LENGTH_LONG).show()
                }

                override fun menuClose() {
                    Toast.makeText(applicationContext, "Menu closed!", Toast.LENGTH_LONG).show()
                }


            }
            menuRed.toggleListener = listener
            progressBarMini.showProgressBar()
            progressBar.setOnClickListener {
                if (!revealed) {
                    progressBar.doReveal(resources.getColor(R.color.fab_reveal_color))
                } else {
                    progressBar.undoReveal()
                }
                revealed = !revealed
            }
            progressBarMini.setOnClickListener {
                progressBar.undoReveal()
            }

            mini.setOnClickListener {
                progressBar.hideProgress()
            }

            miniTwo.setOnClickListener {
                if (miniTwo.isChecked) {
                    progressBar.hideProgress()
                } else {
                    progressBar.showProgressBar()
                }
                miniTwo.isChecked = !miniTwo.isChecked
            }


            fab3.setOnClickListener {
                fab3.isChecked = !fab3.isChecked
            }

            fabRadial1.setOnClickListener {
                radialMenu.showProgressBar()

            }

            fabRadial2.setOnClickListener {
                radialMenu.hideProgress()
            }

            fabRadial3.setOnClickListener {
                fabRadial3.isChecked = !fabRadial3.isChecked
                val drawable =
                    ContextCompat.getDrawable(applicationContext, R.drawable.ic_bookmark_active)
                if (drawable != null) {
                    radialMenu.setIcon(drawable)
                }
            }
        }




    }
}
