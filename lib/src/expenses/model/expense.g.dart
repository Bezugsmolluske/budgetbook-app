// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'expense.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Expense _$ExpenseFromJson(Map<String, dynamic> json) => Expense(
      json['id'] as String,
      (json['version'] as num).toInt(),
      json['date'] as String,
      json['description'] as String,
      json['category'] as String,
      json['amount'] as String,
    );

Map<String, dynamic> _$ExpenseToJson(Expense instance) => <String, dynamic>{
      'id': instance.id,
      'version': instance.version,
      'date': instance.date,
      'description': instance.description,
      'category': instance.category,
      'amount': instance.amount,
    };
