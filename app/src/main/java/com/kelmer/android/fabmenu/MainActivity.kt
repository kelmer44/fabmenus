package com.kelmer.android.fabmenu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    private var revealed : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//
//        fab1.setOnClickListener {
//            menu_red.showProgressBar()
//        }
//
//        fab2.setOnClickListener {
//            menu_red.hideProgress()
//        }
//
//
//        fab3.setOnClickListener {
//
//        }
//
//        menu_red.setClosedOnTouchOutside(true)
//        val listener = object : MenuInterface {
//            override fun menuOpen() {
//                Toast.makeText(applicationContext, "Menu opened!", Toast.LENGTH_LONG).show()
//            }
//
//            override fun menuClose() {
//                Toast.makeText(applicationContext, "Menu closed!", Toast.LENGTH_LONG).show()
//            }
//
//
//        }
//        menu_red.toggleListener = listener
//        progress_bar_mini.showProgressBar()
//        progress_bar.setOnClickListener {
//            if(!revealed) {
//                progress_bar.doReveal(resources.getColor(R.color.fab_reveal_color))
//            }
//            else {
//                progress_bar.undoReveal()
//            }
//            revealed = !revealed
//        }
//        progress_bar_mini.setOnClickListener {
//            progress_bar.undoReveal()
//        }
//
//        mini.setOnClickListener {
//            progress_bar.hideProgress()
//        }
//
//        mini_two.setOnClickListener {
//            if (mini_two.isChecked) {
//                progress_bar.hideProgress()
//            } else {
//                progress_bar.showProgressBar()
//            }
//            mini_two.isChecked = !mini_two.isChecked
//        }
//
//
//        fab3.setOnClickListener {
//            fab3.isChecked = !fab3.isChecked
//        }

//        fab_radial1.setOnClickListener {
//            radial_menu.showProgressBar()
//        }
//
//        fab_radial2.setOnClickListener {
//            radial_menu.hideProgress()
//        }
//
//        fab_radial3.setOnClickListener {
//            fab_radial3.isChecked = !fab_radial3.isChecked
//        }
    }
}
