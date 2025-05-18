// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'expenses.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Expenses _$ExpensesFromJson(Map<String, dynamic> json) => Expenses(
      json['sum'] as String,
      (json['expenses'] as List<dynamic>)
          .map((e) => Expense.fromJson(e as Map<String, dynamic>))
          .toList(),
    );

Map<String, dynamic> _$ExpensesToJson(Expenses instance) => <String, dynamic>{
      'sum': instance.sum,
      'expenses': instance.expenses.map((e) => e.toJson()).toList(),
    };
