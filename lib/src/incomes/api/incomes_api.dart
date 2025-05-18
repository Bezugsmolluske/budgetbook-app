import 'dart:convert';
import 'package:budgetbook_app/src/incomes/model/income.dart';
import 'package:budgetbook_app/src/incomes/model/incomes.dart';
import 'package:http/http.dart' as http;

enum IncomeCategory {
  all('Alle');

  final String value;
  const IncomeCategory(this.value);

  @override
  String toString() => value;
}

Uri getIncomesUrl(
    {required int month, required int year, required IncomeCategory category}) {
  return Uri(
      scheme: 'http',
      host: 'localhost',
      port: 8080,
      path: 'incomes',
      queryParameters: {
        'month': month.toString(),
        'year': year.toString(),
        'category': category.toString()
      });
}

// getIncomes(month: number, year: number, category: string): Observable<IncomeResource[]> {
//   const options = { params: new HttpParams().set('month', month.toString()).set('year', year.toString()).set('category', category) };
//   return this.http.get<IncomeResource[]>('http://localhost:8080/incomes', options);
// }

// getIncome(incomeId: string): Observable<IncomeResource> {
//   return this.http.get<IncomeResource>('http://localhost:8080/incomes/' + incomeId);
// }

// saveNewIncome(income: IncomeResource): Observable<IncomeResource> {
//   return this.http.post<IncomeResource>('http://localhost:8080/incomes', income);
// }

// editIncome(incomeId: string, income: IncomeResource): Observable<IncomeResource> {
//   return this.http.put<IncomeResource>('http://localhost:8080/incomes/' + incomeId, income);
// }

// deleteIncome(incomeId: string): Observable<void> {
//   return this.http.delete<void>('http://localhost:8080/incomes/' + incomeId);
// }

Future<Incomes> getIncomes(
    {required int month,
    required int year,
    required IncomeCategory category}) async {
  final url = getIncomesUrl(month: month, year: year, category: category);
  final response = await http.get(url);
  try {
    var res = json.decode(utf8.decode(response.bodyBytes));
    return Incomes.fromJson(res);
  } catch (err) {
    print(err);
    rethrow;
  }
}
