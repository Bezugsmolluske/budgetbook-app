import 'dart:convert';
import 'package:budgetbook_app/src/incomes/model/income.dart';
import 'package:http/http.dart' as http;

final url = Uri(
    scheme: 'http',
    host: 'localhost',
    port: 8080,
    path: 'incomes',
    queryParameters: {'month': '2', 'year': '2025', 'category': 'Alle'});

Future<List<Income>> getIncomes() async {
  final response = await http.get(url);
  try {
    var responseList =
        json.decode(utf8.decode(response.bodyBytes)) as List<dynamic>;
    return responseList.map((json) => Income.fromJson(json)).toList();
  } catch (err) {
    print(err);
    rethrow;
  }
}
