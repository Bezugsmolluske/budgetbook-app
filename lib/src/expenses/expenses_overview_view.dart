import 'package:budgetbook_app/src/common/widgets/transaction_table.dart';
import 'package:budgetbook_app/src/expenses/model/expenses.dart';
import 'package:flutter/material.dart';

class ExpensesOverviewView extends StatelessWidget {
  const ExpensesOverviewView({super.key, required this.expenses});
  final Expenses expenses;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: TransactionTable(
        totalSum: expenses.sum,
        transactions: expenses.expenses
            .map((expense) => Transaction(
                  date: expense.date,
                  description: expense.description,
                  category: expense.category,
                  amount: expense.amount,
                ))
            .toList(),
      ),
    );
  }
}
