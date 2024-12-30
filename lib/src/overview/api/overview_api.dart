import 'dart:convert';
import 'package:http/http.dart' as http;

import 'package:budgetbook_app/src/overview/model/budget_overview.dart';
import 'package:budgetbook_app/src/overview/model/yearly_overview.dart';

final url = Uri(
    scheme: 'http',
    host: 'localhost',
    port: 8080,
    path: 'overview',
    queryParameters: Map.from({'years': '2024,2023,2022'}));

Future<BudgetOverview> getBudgetOverview() async {
  final response = await http.get(url);
  try {
    var responseList =
        json.decode(utf8.decode(response.bodyBytes)) as List<dynamic>;
    var yearlyOverview = responseList
        .map((overview) => YearlyOverview.fromJson(overview))
        .toList();
    return BudgetOverview(yearlyOverview);
  } catch (err) {
    print(err);
    rethrow;
  }
}
