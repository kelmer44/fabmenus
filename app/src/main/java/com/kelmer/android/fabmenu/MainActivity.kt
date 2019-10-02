package com.kelmer.android.fabmenu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        fab_gooey1.setChecked(true)
//        fab_gooey2.setChecked(true)
//        fab_gooey3.setChecked(true)



        fab1.setOnClickListener {
            menu_red.showProgressBar()
        }

        fab2.setOnClickListener {
            menu_red.hideProgress()
        }


        fab3.setOnClickListener {

        }

        menu_red.setClosedOnTouchOutside(true)
        val listener = object : MenuInterface {
            override fun menuOpen() {
//                Toast.makeText(applicationContext, "Menu opened!", Toast.LENGTH_LONG).show()
            }

            override fun menuClose() {
//                Toast.makeText(applicationContext, "Menu closed!", Toast.LENGTH_LONG).show()
            }

            override fun menuItemClicked(menuItem: Int) {
//                Toast.makeText(applicationContext, "Menu item clicked $menuItem", Toast.LENGTH_LONG)
//                    .show()
            }

        }
        menu_red.toggleListener = listener

        progress_bar_mini.showProgressBar()



        progress_bar.setOnClickListener {
            progress_bar.doReveal(resources.getColor(R.color.fab_reveal_color))
        }
        progress_bar_mini.setOnClickListener {
            progress_bar.undoReveal()
        }

        mini.setOnClickListener {
            progress_bar.hideProgress()
        }

        mini_two.setOnClickListener {
            progress_bar.showProgressBar()
        }

        gooey_menu.showProgressBar()


    }
}
