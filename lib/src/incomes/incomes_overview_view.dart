import 'package:budgetbook_app/src/common/widgets/transaction_table.dart';
import 'package:budgetbook_app/src/incomes/model/incomes.dart';
import 'package:flutter/material.dart';

class IncomesOverviewView extends StatelessWidget {
  const IncomesOverviewView({super.key, required this.incomes});
  final Incomes incomes;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: TransactionTable(
        totalSum: incomes.amount,
        transactions: incomes.incomes
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
