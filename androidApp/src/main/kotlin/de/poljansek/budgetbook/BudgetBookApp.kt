package de.poljansek.budgetbook

import android.app.Application
import de.poljansek.budgetbook.di.initKoin

class BudgetBookApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
