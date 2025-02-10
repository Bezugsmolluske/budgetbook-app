import 'package:budgetbook_app/src/common/widgets/transaction_table.dart';
import 'package:budgetbook_app/src/incomes/model/income.dart';
import 'package:flutter/material.dart';

class IncomesOverviewView extends StatelessWidget {
  const IncomesOverviewView({super.key, required this.incomes});
  final List<Income> incomes;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: TransactionTable(
        totalSum: '2454,10 €',
        transactions: incomes
            .map((income) => Transaction(
                  date: income.date,
                  description: income.description,
                  category: income.category,
                  amount: income.amount,
                ))
            .toList(),
      ),
    );
  }
}
