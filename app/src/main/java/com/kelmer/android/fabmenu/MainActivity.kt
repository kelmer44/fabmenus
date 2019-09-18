package com.kelmer.android.fabmenu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        gooey_menu.listener = object : MenuInterface {
            override fun menuOpen() {
                Toast.makeText(applicationContext, "Menu opened!", Toast.LENGTH_LONG).show()
            }

            override fun menuClose() {
                Toast.makeText(applicationContext, "Menu closed!", Toast.LENGTH_LONG).show()
            }

            override fun menuItemClicked(menuItem: Int) {
                Toast.makeText(applicationContext, "Menu item clicked $menuItem", Toast.LENGTH_LONG).show()
            }

        }
    }
}
