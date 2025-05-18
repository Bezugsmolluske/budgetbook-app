import 'package:budgetbook_app/src/common/widgets/date_selector.dart';
import 'package:budgetbook_app/src/common/widgets/error_state.dart';
import 'package:budgetbook_app/src/common/widgets/loading_state.dart';
import 'package:budgetbook_app/src/incomes/api/incomes_api.dart';
import 'package:budgetbook_app/src/incomes/incomes_overview_view.dart';
import 'package:budgetbook_app/src/incomes/model/incomes.dart';
import 'package:flutter/material.dart';

class IncomesView extends StatefulWidget {
  const IncomesView({super.key});
  static const routeName = '/incomes';

  @override
  State<IncomesView> createState() => _IncomesViewState();
}

class _IncomesViewState extends State<IncomesView> {
  late Future<Incomes> incomes;
  late DateTime selectedDate;

  @override
  void initState() {
    super.initState();
    selectedDate = DateTime.now();
    _loadIncomes();
  }

  void _loadIncomes() {
    setState(() {
      incomes = getIncomes(
        month: selectedDate.month,
        year: selectedDate.year,
        category: IncomeCategory.all,
      );
    });
  }

  void _handleDateChanged(DateTime newDate) {
    setState(() {
      selectedDate = newDate;
      _loadIncomes();
    });
  }

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(builder: (context, constraints) {
      return Scaffold(
        appBar: AppBar(
          title: const Text('Einkommen'),
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
          child: FutureBuilder<Incomes>(
            future: incomes,
            builder: (context, snapshot) {
              if (snapshot.hasData) {
                return IncomesOverviewView(incomes: snapshot.data!);
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
