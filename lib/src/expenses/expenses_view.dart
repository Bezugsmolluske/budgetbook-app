import 'package:budgetbook_app/src/common/widgets/date_selector.dart';
import 'package:budgetbook_app/src/expenses/api/expenses_api.dart';
import 'package:budgetbook_app/src/expenses/expenses_overview_view.dart';
import 'package:budgetbook_app/src/expenses/model/expenses.dart';
import 'package:flutter/material.dart';
import 'package:budgetbook_app/src/common/widgets/loading_state.dart';
import 'package:budgetbook_app/src/common/widgets/error_state.dart';

class ExpensesView extends StatefulWidget {
  const ExpensesView({super.key});
  static const routeName = '/expenses';

  @override
  State<ExpensesView> createState() => _ExpensesViewState();
}

class _ExpensesViewState extends State<ExpensesView> {
  late Future<Expenses> expenses;
  late DateTime selectedDate;

  @override
  void initState() {
    super.initState();
    selectedDate = DateTime.now();
    _loadExpenses();
  }

  void _loadExpenses() {
    setState(() {
      expenses = getExpenses(
        month: selectedDate.month,
        year: selectedDate.year,
        category: ExpenseCategory.all,
      );
    });
  }

  void _handleDateChanged(DateTime newDate) {
    setState(() {
      selectedDate = newDate;
      _loadExpenses();
    });
  }

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(builder: (context, constraints) {
      return Scaffold(
        appBar: AppBar(
          title: const Text('Ausgaben'),
          backgroundColor: Colors.teal,
          actions: [
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16.0),
              child: DateSelector(
                selectedDate: selectedDate,
                onDateChanged: _handleDateChanged,
              ),
            ),
          ],
        ),
        body: SingleChildScrollView(
          child: FutureBuilder<Expenses>(
            future: expenses,
            builder: (context, snapshot) {
              if (snapshot.hasData) {
                return ExpensesOverviewView(expenses: snapshot.data!);
              }
              if (snapshot.hasError) {
                return ErrorState(error: snapshot.error!);
              }
              return const LoadingState();
            },
          ),
        ),
      );
    });
  }
}
