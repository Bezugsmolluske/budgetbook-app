import 'dart:convert';
import 'package:budgetbook_app/src/expenses/model/expenses.dart';
import 'package:http/http.dart' as http;

enum ExpenseCategory {
  all('Alle');

  final String value;
  const ExpenseCategory(this.value);

  @override
  String toString() => value;
}

Uri getExpensesUrl(
    {required int month,
    required int year,
    required ExpenseCategory category}) {
  return Uri(
      scheme: 'http',
      host: 'localhost',
      port: 8080,
      path: 'expenses',
      queryParameters: {
        'month': month.toString(),
        'year': year.toString(),
        'category': category.toString()
      });
}

Future<Expenses> getExpenses({
  required int month,
  required int year,
  required ExpenseCategory category,
}) async {
  final response = await http.get(getExpensesUrl(
    month: month,
    year: year,
    category: category,
  ));

  try {
    final json = jsonDecode(utf8.decode(response.bodyBytes));
    return Expenses.fromJson(json);
  } catch (err) {
    print(err);
    rethrow;
  }
}
